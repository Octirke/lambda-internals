# Lambda internals

## Prérequis

Terraform et le framework serverless sont nécessaires pour déployer rapidement, sinon il faudra tout faire à la main.

## Utilisation

Pour l'instant, seul le code Java est présent.

A la racine du projet, il y a un fichier `ec2.tf` pour monter l'instance ec2 sur le VPC rdyap avec terraform.
Il faut juste spécifier sa propre key qui permettera l'accès en SSH sur l'instance EC2 pour lancer netcat en écoute sur le port 1234 par défaut.

```bash
terraform init
terraform apply
```
 
Puis 
 
```bash
# Se connecter en ssh sur l'instance EC2 puis lancer netcat en écoute
nc -nvlp 1234
```

Ensuite, le dossier java contient le fichier serverless pour bootstrap deux lambdas, une pour lancer le remote shell et l'autre pour télécharger le dossier /var/runtime/lib puis l'upload sur un bucket S3 existant.

La lambda de shell attends un event qui contient le port et l'adresse ou l'IP de l'instance EC2.

La lambda de dll attends un event qui contient le nom du bucket S3 pour l'upload du zip.

Pour déployer et appeler le tout :

```bash
cd java
mvn package
sls deploy

# Si netcat tourne sur EC2 on obtiendra un shell
cat shell-event.json | sls invoke -f shell
cat download-event.json | sls invoke -f upload
```

Voir certains processes de la lambda

```bash
ps -o user -o pid -o command

USER       PID COMMAND
487          1 /usr/bin/java -XX:MaxHeapSize=445645k -XX:MaxMetaspaceSize=52429k -XX:ReservedCodeCacheSize=26214k -Xshare:on -XX:-TieredCompilation -XX:+UseSerialGC -Djava.net.preferIPv4Stack=true -jar /var/runtime/lib/LambdaJavaRTEntry-1.0.jar
487         11 /bin/sh -i
487         15 ps -o user -o pid -o command
```
