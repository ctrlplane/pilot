# pilot
Key provider middleware

### Building from source
```mvn clean compile jib:build```
This builds the image and deploys it to the harbor repository.
No local docker daemon is required for the build.


### Running 
To run with podman: 

```podman run -d --network=host harbor.ctrlplane.net/ctrlplane/pilot:0.0.2-SNAPSHOT```

(Using host mode is the easiest way to connect to copilot on localhost; otherwise change the baseUrl in the configuration)