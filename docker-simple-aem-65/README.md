# General Dev Ops instructions

Note that this capability is in demo form and is not a fully standalone product.
Projects should incorporte this into their code.

## Deployment Guide for interation 1 demo instance
### Approach
Since we're spinning up a quick demo without really any dev ops support, we're packing crx-quickstart and deploying it on Azure as a WebApp

### Steps
1. Have the build you're happy with locally in AEM instance
2. Shutdown AEM locally if it's running
3. Go to root of the author instance (you should see crx-quickstart folder)
4. Run command, this will take a few minutes to complete. Feel free to use cfv param if you like verbose mode.
```shell
cd /Users/jack.jin/accdev/aem-training/2023q2/non-cloud/instance/author
tar cf crx-quickstart.tar crx-quickstart
```
5. Move the tar from author to this folder, sample command
```shell
cd /Users/jack.jin/accdev/aem-training/2023q2/non-cloud/instance/author
mv crx-quickstart.tar ../../../wknd-cloud1/aem-guides-wknd/docker-simple-aem-65/
```
6. If testing is wanted before the whole upload / deploy process, test your image
7. Execute docker build to build your image. Increment version if necessary. If you're on Mac it might be important for you to manually compile into AMD64 and not ARM64
8. Push your image
9. Deploy your image in Azure in a web container with matching version. Currently to save cost we're using a development B2.
