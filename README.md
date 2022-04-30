# pilot
Key provider middleware
## Status
![pilot CI](https://github.com/ctrlplane/pilot/actions/workflows/pilot-build.yaml/badge.svg)

## Running 
To run with podman: 

```podman run -d --network=host quay.io/ctrlplane/pilot:0.0.2-SNAPSHOT```

(Using host mode is the easiest way to connect to copilot on localhost; otherwise change the baseUrl in the configuration)

## Building from source

From the top level of the repository:

```$ mvn -B --file pom.xml```

This will create an executable `.jar` file in the `target` directory.

## License
Copyright © 2022, Control Plane Software, LLC. Released under the [GPL-3.0 License.](https://github.com/ctrlplane/pilot/blob/main/LICENSE)
