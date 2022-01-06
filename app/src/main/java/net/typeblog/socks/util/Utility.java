package net.typeblog.socks.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import net.typeblog.socks.R;
import net.typeblog.socks.SocksVpnService;
import static net.typeblog.socks.util.Constants.*;

public class Utility {
    private static final String TAG = Utility.class.getSimpleName();

    public static int exec(String cmd, boolean wait) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);

            if (wait) {
                return p.waitFor();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int exec(String cmd) {
        return exec(cmd, true);
    }

    public static void killPidFile(String f) {
        File file = new File(f);

        if (!file.exists()) {
            return;
        }

        InputStream i;
        try {
            i = new FileInputStream(file);
        } catch (Exception e) {
            return;
        }

        byte[] buf = new byte[512];
        int len;
        StringBuilder str = new StringBuilder();

        try {
            while ((len = i.read(buf, 0, 512)) > 0) {
                str.append(new String(buf, 0, len));
            }
            i.close();
        } catch (Exception e) {
            return;
        }

        try {
            int pid = Integer.parseInt(str.toString().trim().replace("\n", ""));
            Runtime.getRuntime().exec("kill " + pid).waitFor();
            if(!file.delete())
                Log.w(TAG, "failed to delete pidfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String join(List<String> list, String separator) {
        if (list.isEmpty()) return "";

        StringBuilder ret = new StringBuilder();

        for (String s : list) {
            ret.append(s).append(separator);
        }

        return ret.substring(0, ret.length() - separator.length());
    }

    public static void makeCsnetConf(Context context, String server, int port, boolean ipv6) {
        // 生成配置文件
        String conf = context.getString(R.string.csnet_conf)
                .replace("{SERVER}", server)
                .replace("{PORT}", Integer.toString(port))
                .replace("{IP_VERSION}", Integer.toString(ipv6 ? 0 : 1));

        File f = new File(context.getFilesDir() + "/csnet_client_config.ini");

        if (f.exists()) {
            if(!f.delete())
                Log.w(TAG, "failed to delete csnet_client_config.ini");
        }

        try {
            OutputStream out = new FileOutputStream(f);
            out.write(conf.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int startCsnet(Context context) {
        String cmd = context.getApplicationInfo().nativeLibraryDir + "/libcsnet.so -c " + context.getFilesDir() + "/csnet_client_config.ini";
        Log.d(TAG, "starting csnet: " + cmd);
        return exec(cmd, false);
    }

    public static int stopCsnet() {
        Log.d(TAG, "stopping csnet...");
        return exec("killall libcsnet.so");
    }

    public static void makePdnsdConf(Context context, String dns, int port) {
        String conf = context.getString(R.string.pdnsd_conf)
                .replace("{DIR}", context.getFilesDir().toString())
                .replace("{IP}", dns)
                .replace("{PORT}", Integer.toString(port));

        File f = new File(context.getFilesDir() + "/pdnsd.conf");

        if (f.exists()) {
            if(!f.delete())
                Log.w(TAG, "failed to delete pdnsd.conf");
        }

        try {
            OutputStream out = new FileOutputStream(f);
            out.write(conf.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File cache = new File(context.getFilesDir() + "/pdnsd.cache");

        if (!cache.exists()) {
            try {
                if(!cache.createNewFile())
                    Log.w(TAG, "failed to create pdnsd.cache");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startVpn(Context context, Profile profile, Handler handler) {
        class StartVpnThread implements Runnable {
            Context context;
            Profile profile;
            Handler handler;

            public StartVpnThread(Context context, Profile profile, Handler handler) {
                this.context = context;
                this.profile = profile;
                this.handler = handler;
            }

            @Override
            public void run() {
                String server = profile.getServer();
                try {
                    Log.i(TAG, "server host: " + server);
                    server = Inet4Address.getByName(server).getHostAddress();
                    Log.i(TAG, "server ip: " + server);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    if (handler != null) {
                        String toast = context.getResources().getString(R.string.cannot_resolve_host, server);
                        Bundle data = new Bundle();
                        data.putBoolean("success", false);
                        data.putString("toast", toast);
                        Message msg = new Message();
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                    return;
                }

                Intent i = new Intent(context, SocksVpnService.class)
                        .putExtra(INTENT_NAME, profile.getName())
                        .putExtra(INTENT_SERVER, server)
                        .putExtra(INTENT_PORT, profile.getPort())
                        .putExtra(INTENT_ROUTE, profile.getRoute())
                        .putExtra(INTENT_DNS, profile.getDns())
                        .putExtra(INTENT_DNS_PORT, profile.getDnsPort())
                        .putExtra(INTENT_PER_APP, profile.isPerApp())
                        .putExtra(INTENT_IPV6_PROXY, profile.hasIPv6());

                if (profile.isUserPw()) {
                    i.putExtra(INTENT_USERNAME, profile.getUsername())
                            .putExtra(INTENT_PASSWORD, profile.getPassword());
                }

                if (profile.isPerApp()) {
                    i.putExtra(INTENT_APP_BYPASS, profile.isBypassApp())
                            .putExtra(INTENT_APP_LIST, profile.getAppList().split("\n"));
                }

                if (profile.hasUDP()) {
                    i.putExtra(INTENT_UDP_GW, profile.getUDPGW());
                }

                context.startService(i);

                if (handler != null) {
                    Bundle data = new Bundle();
                    data.putBoolean("success", true);
                    Message msg = new Message();
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
        }

        new Thread(new StartVpnThread(context, profile, handler)).start();
    }
}
