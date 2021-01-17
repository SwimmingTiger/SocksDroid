package net.typeblog.socks.util;

import android.content.Context;
import android.net.VpnService;
import android.util.Log;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import net.typeblog.socks.R;
import static net.typeblog.socks.util.Constants.*;

public class Routes {
    public static void addRoutes(Context context, VpnService.Builder builder, String name, String server) {
        try {
            InetAddress ip = InetAddress.getByName(server);
            int ipInt = ByteBuffer.wrap(ip.getAddress()).getInt();

            // 通过路由规则排除server，转发所有其他IP到VPN
            for (int i = 1; i <= 32; i++) {
                int shift = 32 - i;
                int subnetInt = ((ipInt >> shift) ^ 1) << shift;
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.putInt(subnetInt);
                InetAddress subnetIP = InetAddress.getByAddress(buffer.array());
                String subnetIPStr = subnetIP.getHostAddress();
                builder.addRoute(subnetIPStr, i);
                Log.d("addRoutes", subnetIPStr + "/" + String.valueOf(i));
            }
        } catch (Exception ex) {
            Log.e("addRoutes", ex.toString());
            ex.printStackTrace();
        }
    }
}
