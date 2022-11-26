# Prerequisites

The following instructions asusme that containerd is your only Kubernetes container runtime. The use of multiple container runtimes is outside the scope of these instructions. We assume the following is already complete:

- containerd installed with all of its prerequisites. Instructions can be found [here](https://github.com/containerd/containerd/blob/main/docs/getting-started.md)
- [containerd-imgcrypt](https://github.com/containerd/imgcrypt) installed. Instructions can be found [here](https://github.com/containerd/imgcrypt#usage)
    - **NOTE**: *imgcrypt requires that golang and gcc are installed in order to compile* 

# Prepare Worker Nodes

## Verify containerd Configuration

1. Create configuration directory for containerd:
```
$ sudo mkdir -p /etc/containerd && sudo mkdir /etc/containerd/ocicrypt
```
2. Dump containerd default config:
```
$ containerd config default > /etc/containerd/config.toml
```
You should see the following section in the `config.toml`:
```
[stream_processors]

  [stream_processors."io.containerd.ocicrypt.decoder.v1.tar"]
    accepts = ["application/vnd.oci.image.layer.v1.tar+encrypted"]
    args = ["--decryption-keys-path", "/etc/containerd/ocicrypt/keys"]
    env = ["OCICRYPT_KEYPROVIDER_CONFIG=/etc/containerd/ocicrypt/ocicrypt_keyprovider.conf"]
    path = "ctd-decoder"
    returns = "application/vnd.oci.image.layer.v1.tar"

  [stream_processors."io.containerd.ocicrypt.decoder.v1.tar.gzip"]
    accepts = ["application/vnd.oci.image.layer.v1.tar+gzip+encrypted"]
    args = ["--decryption-keys-path", "/etc/containerd/ocicrypt/keys"]
    env = ["OCICRYPT_KEYPROVIDER_CONFIG=/etc/containerd/ocicrypt/ocicrypt_keyprovider.conf"]
    path = "ctd-decoder"
    returns = "application/vnd.oci.image.layer.v1.tar+gzip"
```
3. Verify that `ctd-decoder` is in your $PATH:
```
$ which ctd-decoder
/usr/local/bin/ctd-decoder
```
4. Place your `ocicrypt_keyprovider.conf`:
```
cat <<EOF >> /etc/containerd/ocicrypt/ocicrypt_keyprovider.conf
{
    "key-providers": {
        "pilot": {
            "grpc": "192.168.1.1:30051" # address or DNS name of your pilot gRPC service
        }
    }
}
EOF
```
# Test Functionality

## Encrypt an image

Encrypt a test image using `skopeo` and push it to your registry:
```
$ cat ocicrypt.conf
{
    "key-providers": {
        "pilot": {
            "grpc": "192.168.1.1:30051" # address or DNS name of your pilot gRPC service
        }
    }
}
$ OCICRYPT_KEYPROVIDER_CONFIG=ocicrypt.conf skopeo copy --encryption-key provider:pilot:testkey oci:nginx:latest docker:
//harbor.ctrlplane.net/ctrlplane/nginx-encrypted:1.0
```