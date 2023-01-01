## Chaos Operator

K8s operator for supervised chaos engineering.

### Supervised Chaos Engineering
By that term we understand chaos engineering without "random" part. Sometimes it's usable to be able to
apply different chaos actions in controlled way.

### Installation
Either by applying single manifests from `crds` directory or by helm (easier). To install by helm, add repository:
```bash
helm repo add <repository name> https://davenury.github.io/Chaos-Operator
```
and then install release:

```bash
helm install <release name> <repository name>/chaos-operator
```

You can also customize your deployment, by providing file with values. Given a file named `values.yaml` in your current directory:
```yaml
namespace:
  name: chaos-operator    # namespace to deploy operator and samples, if applied
  enable: true            # whether to create namespace

operator:
  deployment_name: chaos-operator         # name of deployment 
  image: ghcr.io/davenury/chaos-operator:latest   # image of chaos operator, for specific versions, head to: https://github.com/users/Davenury/packages/container/package/chaos-operator

serviceAccount:
  name: chaos-operator      # name of the service account
  enable: true              # whether to create the service account
  clusterRole:
    name: chaos-operator    # name of the cluster role

samples:                    # whether to create a sample applications to demonstrate the capabilities of the operator
  deployment: true          # creates sample-deployment, used by default scenario in the first phase
  single_pod: true          # creates nginx pod, used by default scenario in the second phase
  service: true             # creates sample-service, used by default scenario in the third phase
```
you can pass new values while installing the release:
```bash
helm install <release name> <repository name>/chaos-operator -f values.yaml
```

Helm chart will create chaos-operator deployment, as well as needed resources (those can be disabled by modifying `values.yaml` file).
To execute sample scenario, enter the `crds` directory and:
```bash
kubectl apply -f sample.yaml
```

### Scenario definition
Sample definition is like this:
```yaml
apiVersion: lsc.davenury.github.com/v1
kind: Scenario
metadata:
  name: sample
  namespace: chaos-operator
spec:
  phases:
    - duration: PT60S
      actions:
      - namespace: chaos-operator
        resourceType: deployment
        resourceName: sample-deployment
        action: scale
        value: 2
    - duration: PT30S
      actions:
        - namespace: chaos-operator
          resourceType: pod
          resourceName: nginx
          action: delete
    - duration: PT20S
      actions:
        - namespace: chaos-operator
          resourceType: service
          resourceName: my-service
          action: delete
```
`Spec` is composed of `phases`. A `phase` is a set of `actions` applied at the same time throughout the same span of time.
A `phase` is composed of `duration` (ISO-8601 compatible) and `actions` - a list of `action`.
`action` refers to single action performed on cluster, e.g. deletion of a pod or scaling of a deployment. Each action requires:
* namespace - the namespace of the resource
* resourceType - k8s resource type (e.g. deployment, pod, service)
* resourceName - the name of the resource
* action - a verb that describes what should happen (e.g. scale, delete)
Additional param for `scale` action: value - describes the desired value of the scale action.

### Available actions
* delete pod
* delete service
* scale deployment
