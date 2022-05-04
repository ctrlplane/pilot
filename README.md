# pilot
Key provider middleware
## Status
![pilot CI](https://github.com/ctrlplane/pilot/actions/workflows/pilot-build.yaml/badge.svg)

## Running 
To run with podman: 

```podman run -d --network=host quay.io/ctrlplane/pilot:latest```

(Using host mode is the easiest way to connect to copilot on localhost; otherwise change the baseUrl in the configuration)

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

## License
Copyright © 2022, Control Plane Software, LLC. Released under the [GPL-3.0 License.](https://github.com/ctrlplane/pilot/blob/main/LICENSE)
