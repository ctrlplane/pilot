# pilot
## Status
Key provider middleware

## Running 
To run with podman: 

```podman run -d --network=host harbor.ctrlplane.net/ctrlplane/pilot:0.0.2-SNAPSHOT```

(Using host mode is the easiest way to connect to copilot on localhost; otherwise change the baseUrl in the configuration)

## Building from source

From the top level of the repository:

```$ mvn clean compile jib:build```

This builds the image and deploys it to the harbor repository.
No local docker daemon is required for the build.

## License
Copyright © 2022, Control Plane Software, LLC. Released under the [GPL-3.0 License.](https://github.com/ctrlplane/pilot/blob/main/LICENSE)
