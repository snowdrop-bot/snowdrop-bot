# Helm charts

This chart deploys snowdrop-bot on a [Kubernetes](http://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites

- Kubernetes v1.18+

## Installing the Chart

To install the chart on the cluster:

```console
$ kubectl create ns bot
$ helm install snowdrop-bot -n bot ./deploy/charts -f my-values.yml
```

## Uninstalling the Chart

To uninstall/delete:

```console
$ helm uninstall -n bot snowdrop-bot
```

The command removes all the Kubernetes resources associated with the chart and deletes the helm release.

## Configuration

The following table lists the configurable parameters of the `snowdrop-bot` chart, and their default values.

### Common
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| appVersion | string | `"latest"` |  |
| fullnameOverride | string | `""` |  |
| nameOverride | string | `""` |  |

### Deployment
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| deployment.affinity | object | `{}` |  |
| deployment.containerPort | int | `8080` |  |
| deployment.env[0].name | string | `"JAVA_OPTIONS"` |  |
| deployment.env[0].value | string | `"-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"` |  |
| deployment.image.pullPolicy | string | `"Always"` |  |
| deployment.image.repository | string | `"quay.io/snowdrop/snowdrop-bot"` |  |
| deployment.image.tag | string | `"latest"` |  |
| deployment.imagePullSecrets | list | `[]` |  |
| deployment.nodeSelector | object | `{}` |  |
| deployment.podAnnotations | object | `{}` |  |
| deployment.podSecurityContext | object | `{}` |  |
| deployment.replicaCount | int | `1` |  |
| deployment.resources | object | `{}` |  |
| deployment.securityContext.runAsUser | int | `1001` |  |
| deployment.tolerations | list | `[]` |  |

### Ingress
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| ingress.annotations | object | `{}` |  |
| ingress.enabled | bool | `true` |  |
| ingress.hosts[0].host | string | `"snowdrop-bot.195.201.87.126.nip.io"` |  |
| ingress.hosts[0].paths[0].path | string | `"/"` |  |
| ingress.hosts[0].paths[0].pathType | string | `"Prefix"` |  |
| ingress.tls | list | `[]` |  |

### Persistent Volume claim
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| pvc.create | bool | `true` |  |
| pvc.name | string | `"snowdrop-bot-claim"` |  |

### Secret
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| secret.github.associates | string | `"aureamunoz,cmoulliard,iocanel,geoand,metacosm,gytis,jacobdotcosta,BarDweller"` |  |
| secret.github.token | string | `nil` |  |
| secret.jira.password | string | `nil` |  |
| secret.jira.username | string | `nil` |  |

### Service
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |

### Service Account
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| serviceAccount.annotations | object | `{}` |  |
| serviceAccount.create | bool | `true` |  |
| serviceAccount.name | string | `""` |  |

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`. For example:

```console
$ helm install snowdrop-bot ./deploy/charts -n bot --set deployment.replicaCount=1
```