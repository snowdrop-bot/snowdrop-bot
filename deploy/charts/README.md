# Snowdrop Bot Helm Charts

## Introduction

This chart deploys the Snowdrop bot application on a [Kubernetes](http://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites

- Kubernetes v1.18+

## Installing the Chart

To install the snowdrop-bot chart:

```console
$ helm install snowdrop-bot -n my-namespace snowdrop-bot ./deploy/charts
```

## Uninstalling the Chart

To uninstall/delete the release:

```console
$ helm delete -n my-namespace snowdrop-bot
```

The command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration

The following table lists the configurable parameters of the `snowdrop-bot` chart, and their default values.

|              Parameter               |                                                      Description                                                       |                                    Default                                     |
|--------------------------------------|------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| nameOverride                         | Section containing properties which can be used by the different resources                                             | `""`                                                                           |
| fullnameOverride                     |                                                                                                                        | `""`                                                                           |
| appVersion                           |                                                                                                                        | `"latest"`                                                                     |
| deployment.replicaCount              |                                                                                                                        | `1`                                                                            |
| deployment.image.repository          |                                                                                                                        | `quay.io/snowdrop/snowdrop-bot`                                                |
| deployment.image.pullPolicy          |                                                                                                                        | `Always`                                                                       |
| deployment.image.tag                 | Overrides the image tag whose default is the chart appVersion.                                                         | `"latest"`                                                                     |
| deployment.containerPort             |                                                                                                                        | `8080`                                                                         |
| deployment.imagePullSecrets          |                                                                                                                        | `[]`                                                                           |
| deployment.podAnnotations            |                                                                                                                        | `{}`                                                                           |
| deployment.podSecurityContext        |                                                                                                                        | `{}`                                                                           |
| deployment.securityContext.runAsUser | capabilities: drop: - ALL readOnlyRootFilesystem: true runAsNonRoot: true                                              | `1001`                                                                         |
| deployment.resources                 |                                                                                                                        | `{}`                                                                           |
| deployment.nodeSelector              |                                                                                                                        | `{}`                                                                           |
| deployment.tolerations               |                                                                                                                        | `[]`                                                                           |
| deployment.affinity                  |                                                                                                                        | `{}`                                                                           |
| service.type                         |                                                                                                                        | `ClusterIP`                                                                    |
| service.port                         |                                                                                                                        | `8080`                                                                         |
| ingress.enabled                      |                                                                                                                        | `true`                                                                         |
| ingress.annotations                  |                                                                                                                        | `{}`                                                                           |
| ingress.tls                          |                                                                                                                        | `[]`                                                                           |
| pvc.create                           | Specifies whether a pvc should be created                                                                              | `true`                                                                         |
| pvc.name                             | The name of the pvc  to use. If not set and create is true, a name is generated using the fullname template            | `"snowdrop-bot-claim"`                                                         |
| serviceAccount.create                | Specifies whether a service account should be created                                                                  | `true`                                                                         |
| serviceAccount.annotations           | Annotations to add to the service account                                                                              | `{}`                                                                           |
| serviceAccount.name                  | The name of the service account to use. If not set and create is true, a name is generated using the fullname template | `""`                                                                           |
| secret.jira.username                 |                                                                                                                        | ``                                                                             |
| secret.jira.password                 |                                                                                                                        | ``                                                                             |
| secret.jira.users                    |                                                                                                                        | `""`                                                                           |
| secret.github.token                  |                                                                                                                        | ``                                                                             |
| secret.github.associates             |                                                                                                                        | `aureamunoz,cmoulliard,iocanel,geoand,metacosm,gytis,jacobdotcosta,BarDweller` |


Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`. For example:

```console
$ helm install snowdrop-bot -n my-namespace ./deploy/charts --set appVersion="latest"
```

Alternatively, a YAML file that specifies the values for the parameters can be provided while
installing the chart. For example:

```console
$ helm install snowdrop-bot -n my-namespace ./deploy/charts -f my-values.yaml
```

## How To document me

This page has been generated using the tool [chart-doc-gen](https://github.com/kubepack/chart-doc-gen) and template files available under `./doc`
```bash
chart-doc-gen -v ./values.yaml -d ./doc/doc.yaml -t ./doc/doc.tmpl> README.md
```

