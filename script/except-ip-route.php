<?php
$ip = "192.168.240.90";

$long = ip2long($ip);

for ($i=1; $i<=32; $i++) {
    $shift = 32 - $i;
    $subnet = (($long >> $shift) ^ 1) << $shift;
    $subnetLong = long2ip($subnet);
    echo ip2bin($subnetLong), "\t", $subnetLong, "/$i\n";
}

function ip2bin($ip) {
    $parts = explode('.', $ip);
    $results = [];
    foreach ($parts as $part) {
        $results[] = str_pad(decbin($part), 8, '0', STR_PAD_LEFT);
    }
    return implode('.', $results);
}
