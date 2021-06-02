# Linux 7 集群安装MinIO文件服务器

### 一、安装环境要求

Linux 7 版本 

go版本：[go1.10.3](https://dl.google.com/go/go1.10.3.linux-amd64.tar.gz)

MinIO：最新版本

因为MinIO是用go语音编写的，所有服务器需要安装GO环境

### 二、安装GO环境

```xml-dtd
wget --no-check-certificate https://dl.google.com/go/go1.10.3.linux-amd64.tar.gz
```

解压配置

```xml-dtd
tar -zxvf  go1.10.3.linux-amd64.tar.gz  -C /usr/local
vim /etc/profile

export GO_HOME=/usr/local/go
export PATH=$PATH:$GO_HOME/bin


source /etc/profile
```

验证

```xml-dtd
go version
```

### 三、安装MinIO

1、下载地址：https://dl.minio.io/server/minio/release/linux-amd64/minio

推荐下载本地后上传方式，巨慢

```xml-dtd
wget https://dl.minio.io/server/minio/release/linux-amd64/minio
```

2、创建文件夹

```xml-dtd
mkdir -p /usr/local/src/minio/
```

3、上传 minio 文件，修改权限：进入到minio 执行

```xml-dtd
chmod +x minio
```

以上命令在多个节点都需要执行

### 四、分布式搭建准备(双节点)

minio分布式部署最少要求4个存储，资源限制，我采用了两台服务器双盘存储

创建存储空间：

```xml-dtd
192.168.234.20
mkdir /data/{export1,export2}

192.168.234.5
mkdir /data/{export3,export4}
```

### 五、启动服务

```xml-dtd
cd /usr/local/src/minio/
# 设置用户名密码，两台服务器需要一致
export MINIO_ACCESS_KEY=admin
export MINIO_SECRET_KEY=admin123
# 直接启动
./minio server http://192.168.234.20/data/export1 http://192.168.234.20/data/export2 http://192.168.234.5/data/export3 http://192.168.234.5/data/export4
# 后台启动（推荐）
nohup ./minio server http://192.168.234.20/data/export1 http://192.168.234.20/data/export2 http://192.168.234.5/data/export3 http://192.168.234.5/data/export4 &
```

### 六、服务验证

```xml-dtd
ps -ef|grep minio
```

**注意: 两个服务器的时间必须一致不然报错**

**时间同步**

```xml-dtd
ntpdate cn.pool.ntp.org
```

### 七、Nginx 负载均衡

```xml-dtd
/usr/local/nginx/sbin/nginx -t -c /usr/local/nginx/conf/nginx.conf
upstream http_minio {
    server 192.168.234.20:9000;
    server 192.168.234.5:9000;
}

server{
    listen       80;
    server_name  localhost;

    ignore_invalid_headers off;
    client_max_body_size 0;
    proxy_buffering off;

    location /minio {
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-Host  $host:$server_port;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto  $http_x_forwarded_proto;
        proxy_set_header   Host $http_host;

        proxy_connect_timeout 300;
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
        proxy_ignore_client_abort on;

        proxy_pass http://http_minio;
    }
}
```

 proxy_pass 的值为 http://http_minio ,也就是上面设置的 upstream **http_minio**  值

其中主要是 upstream 及 proxy_pass 的配置。如此，即可使用 http://192.168.234.20 进行访问。
