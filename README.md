# pilot
`pilot` is a key provider middleware (KPM) that can be used alongside your existing container tooling to add OCI-compliant container image layer encryption and decryption capabilties. `pilot` can also be deployed to production environments to provide on-pull image decryption by sending requests to `copilot` to retrieve key encryption keys (KEKs) from your key management system (KMS).

`pilot` is compatible with any tools or runtimes that leverage the [ocicrypt](https://github.com/containers/ocicrypt) `keyprovider` protocol. Currently, these include:

- [skopeo](https://github.com/containers/skopeo)
- [containerd](https://github.com/containerd/containerd)
- [cri-o](https://github.com/cri-o/cri-o)
- [imgcrypt](https://github.com/containerd/imgcrypt)

## Status
![pilot CI](https://github.com/ctrlplane/pilot/actions/workflows/build.yaml/badge.svg)
[![CodeQL](https://github.com/ctrlplane/pilot/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/ctrlplane/pilot/actions/workflows/codeql-analysis.yml)

## Quick Start 
To run with podman: 

```podman run -d --network=host quay.io/ctrlplane/pilot:latest```

(Using host mode is the easiest way to connect to copilot on localhost; otherwise change the baseUrl in the configuration)

*NOTE: `pilot` cannot currently be run standalone. You must have an instance of `copilot` running to service key requests. See [this page](https://github.com/ctrlplane/copilot) for instructions.*

## Building from source

From the top level of the repository:

```$ mvn clean package```

A successful build should output something like:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.979 s
[INFO] Finished at: 2022-05-02T20:33:57-04:00
[INFO] ------------------------------------------------------------------------
```

This will create an executable `.jar` file in the `target` directory.

## Key Encryption

For more detailed information about container image encryption, see the [ocicrypt reposiroty](https://github.com/containers/ocicrypt). `pilot` provides key wrapping, or key encryption, which protects the the symetric keys created during the encryption of each image layer.

Currently, `pilot` supports the following key encryption strategies:

* AES256-GCM Symetric Encryption

## License
Copyright © 2022, Control Plane Software, LLC. Released under the [GPL-3.0 License.](https://github.com/ctrlplane/pilot/blob/main/LICENSE)
