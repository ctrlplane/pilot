# Prerequisites

Pilot is compatible with any tools or runtimes that leverage the [ocicrypt](https://github.com/containers/ocicrypt) `keyprovider` protocol. Currently, these include:

- [skopeo](https://github.com/containers/skopeo)
- [containerd](https://github.com/containerd/containerd)
- [cri-o](https://github.com/cri-o/cri-o)
- [imgcrypt](https://github.com/containerd/imgcrypt)

# Running `pilot`

## Configure Tooling to use `pilot`

Depending on the container tooling and/or runtime you use, the location of `ocicrypt.conf` will vary. If using `pilot` for CI or image building and shipping, you can simply create the config in your home directory:
```
cat <<EOF > ${HOME}/ocicrypt.conf
{
    "key-providers": {
        "pilot": {
            "grpc": "localhost:50051"
        }
    }
}
EOF   
```

## Standalone

The easiest way to run `pilot` is the use Docker or Podman to create a local gRPC server on the node you'd like to use for image encryption and decryption. For example, you may have a development or build machine where you'd like access to your team's image encryption resources.

1. Start `pilot`, being sure to set the `GRPC_PORT` environment variable. This is the port on which `pilot` will listen for incoming `keyWrap` requests from your container tooling or runtime. *The default gRPC port is `50051`.* Also set the `KEYREQUEST_BASEURL` environment variable to the URL of `copilot`'s API endpoint. *The default URL is `http://localhost:8080/api/v1/key`.*
```
$ podman run -d --network=host -e GRPC_PORT=50041 -e KEYREQUEST_BASEURL=http://localhost:8080/api/v1/key quay.io/ctrlplane/pilot:0.0.2-SNAPSHOT
```
2. Start `copilot`
```
$ podman run -d --network=host quay.io/ctrlplane/copilot:0.0.1-SNAPSHOT
```
3. Encrypt an image
```
$ OCICRYPT_KEYPROVIDER_CONFIG=ocicrypt.conf skopeo copy --encryption-key provider:pilot:testkey oci:alpine oci:encrypted
```
4. Decrypt and image
```
$ OCICRYPT_KEYPROVIDER_CONFIG=ocicrypt.conf skopeo copy --decryption-key provider:pilot:testkey oci:encrypted oci:not_encrypted
```

## Kubernetes

Use the provided manifest `pilot.yaml` as a refernce point to create an instance of `pilot` in your Kubernetes environment. See the [copilot repository](https://github.com/ctrlplane/copilot) for details on deploying `copilot` in your on-prem or VPC environment.

1. Create the `ctrlplane-system` namespace
```
$ kubectl create namespace ctrlplane-system
```
2. Deploy pilot
```
$ kubectl apply -f https://github.com/ctrlplane/pilot/blob/main/deploy/pilot.yaml
```
3. Verify the deployment
```
$ kubectl --namespace ctrlplane-system get all -l app=pilot
NAME                                   READY   STATUS    RESTARTS   AGE
pod/ctrlplane-pilot-6b96d97cf6-c45v4   1/1     Running   0          29h

NAME                          TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)           AGE
service/ctrlplane-pilot-svc   NodePort   10.43.26.56   <none>        30051:30051/TCP   29h

NAME                              READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/ctrlplane-pilot   1/1     1            1           29h

NAME                                         DESIRED   CURRENT   READY   AGE
replicaset.apps/ctrlplane-pilot-6b96d97cf6   1         1         1       29h
```