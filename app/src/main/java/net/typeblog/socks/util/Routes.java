package net.typeblog.socks.util;

import android.content.Context;
import android.net.VpnService;
import android.util.Log;

import net.typeblog.socks.R;
import static net.typeblog.socks.util.Constants.*;

public class Routes {
    public static void addRoutes(Context context, VpnService.Builder builder, String name, String server) {
        long serverIpInt = ip2long(server);

        if(ROUTE_CHN.equals(name)) {
            addRoutingTable(context, R.array.simple_route, builder, serverIpInt);
        } else {
            routingTableExcludeIp(builder, serverIpInt, 0);
        }
    }

    // 添加路由表到VPN
    private static void addRoutingTable(Context context, int routingTableId, VpnService.Builder builder, long serverIpInt) {
        String[] routes = context.getResources().getStringArray(routingTableId);
        for (String r : routes) {
            String[] cidr = r.split("/");
            if (cidr.length != 2) {
                Log.e("addRoutingTable", "Unknown route format: " + r);
                continue;
            }
            String subnet = cidr[0];
            long subnetInt = ip2long(subnet);
            int prefixLength = Integer.parseInt(cidr[1]);
            int suffixLength = 32 - prefixLength;

            if (serverIpInt != 0 && (serverIpInt >> suffixLength) == (subnetInt >> suffixLength)) {
                routingTableExcludeIp(builder, serverIpInt, prefixLength);
            } else {
                builder.addRoute(subnet, prefixLength);
                Log.d("addRoutingTable", subnet + "/" + String.valueOf(prefixLength));
            }
        }
    }

    // 通过路由规则从指定网段排除server，转发所有其他IP到VPN
    private static void routingTableExcludeIp(VpnService.Builder builder, long serverIpInt, int prefixLength) {
        for (int i = prefixLength + 1; i <= 32; i++) {
            int shift = 32 - i;
            long subnetInt = ((serverIpInt >> shift) ^ 1) << shift;
            String subnetIPStr = long2ip(subnetInt);
            builder.addRoute(subnetIPStr, i);
            Log.d("routingTableExcludeIp", subnetIPStr + "/" + String.valueOf(i));
        }
    }

    public static long ip2long(String ip) {
        String[] addrArray = ip.split("\\.");
        long num = 0;
        for (int i=0;i<addrArray.length;i++) {
            int power = 3-i;
            num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));
        }
        return num;
    }

    public static String long2ip(long ipLong){
        return ((ipLong >> 24) & 0xFF) + "." +
                ((ipLong >> 16) & 0xFF) + "." +
                ((ipLong >> 8) & 0xFF) + "." +
                (ipLong & 0xFF);
    }
}
