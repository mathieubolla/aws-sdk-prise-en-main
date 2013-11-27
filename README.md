AWS Java SDK Prise en main
===

Ce repository contient les différentes étapes de ma présentation au Brown Bag Lunch Lille, chez Adéo, le 4 Décembre 2013. Il permet une prise en main du Amazon Web Services Java SDK, autour d'un exemple d'application distribuée "Jouons à Maven, téléchargeons Internet".

L'idée est de coordonner avec notre machine "maître" une armée de "zombies" (des instances Amazon EC2 sans login) afin que ceux-ci téléchargent récursivement toute page donnée en entrée, et en suivant tous les liens rencontrés dans le contenu, jusqu'a une certaine limite fixée dans le "maître". La coordination se fait via Amazon SQS, et les données collectées sont copiées dans Amazon S3. Ce dernier sers également à stocker le code des "zombies", de manière sécurisée en s'appuyant sur les roles Amazon IAM.

Liste des tags
===

Les tags sur la branche "master" servent à identifier certaines étapes clef dans notre progression. Voici un petit listing des tags et de ce qu'ils représentent.

hello
---

Ce tag représente un premier contact avec Amazon S3, en effectuant un listing des buckets Amazon S3 présents dans votre compte Amazon. Il est possible que rien ne s'affiche, si vous n'avez jamais utilisé S3. Dans ce cas, suivez le reste de la présentation et recommencez: Le bucket créé en fin de présentation sera alors listé.

Pour démarrer:

    mvn exec:java -Dexec.mainClass="com.projet.awssdk.Demo"

hello-s3-world
---

Ce tag représente une utilisation un peu plus concréte, avec la mise à disposition d'un fichier sur S3, dans le bucket `code.projet.com`, avec la clef `hello.txt`. Vous pouvez vérifier sa présence dans la console Amazon S3 [là](https://console.aws.amazon.com/s3/home?region=eu-west-1).

Pour démarrer:

    ./sh/start.sh

A partir de maintenant, toutes les étapes utilisent ce script de lancement.

dead-zombies
---

Ici, une première utilisation d'Amazon EC2 en mode "zombie". Nous allons lancer des instances EC2 sur lesquelles personne n'à le contrôle (aucun accès possible via SSH ou autre). Le principe est de disposer d'executants, les "zombies", qui vont prendre plus tard leurs commandes suivant un protocole prédéfini, ou bien s'arrêter. De cette manière, il est impossible qu'un attaquant prenne le contrôle de nos executants. Dans cette étape, les "zombies" n'ont qu'une vocation: Mourrir. Cette action est programmée via un "startup script", script shell lancé au premier démarrage de l'instance. Pour vérifier que quelque chose à bien démarré et qu'il s'est arrêté, la console Amazon EC2 est [là](https://console.aws.amazon.com/ec2/v2/home?region=eu-west-1). Surveillez notamment les "running instances" et les "volumes". C'est ce qui sera créé à cette étape et qui peux coûter cher sur la durée.

safe-zombies
---

Là, première utilisation de la sécurité des services, Amazon Identity and Access Management ou IAM. Nous allons demander à ce que le service EC2 soit autorisé à recevoir un "role", via un "instance profile". Ce "role" recoit des "policy", des documents qui décrivent des autorisation d'accès. Ensuite, toute instance EC2 qui se voit assigner un et un seul rôle peut endosser ce rôle et travailler avec les autorisations données. Côté SDK, il suffit d'utiliser le `DefaultAWSCredentialsProviderChain` pour que le SDK utilise automatiquement le rôle assigné à la machine. Nos zombies vont donc pouvoir télécharger du code via un utilitaire fourni [là](https://github.com/mathieubolla/aws-sdk-bootstraper), et executer ce code avec les droits spécifiés, à savoir écrire "was there" dans un fichier daté dans le répertoire "/results" du bucket S3. Pour suivre l'apparition des fichiers sur Amazon S3 Console, c'est [ici](https://console.aws.amazon.com/s3/home?region=eu-west-1).

coop-zombies
---

Maintenant, utilisons Amazon SQS pour coordonner nos zombies. Nous allons envoyer des commandes sur une queue, `COMMANDS_QUEUE`, et recevoir les rapports d'avancement sur une seconde, `CONQUESTS_QUEUE`. Le protocole est très simple: Quand un zombie recoit un message sur la `COMMANDS_QUEUE`, il écrit "Was there" dans un fichier daté, et réponds "Roger!" dans la `CONQUESTS_QUEUE`. Quand le contrôleur reçoit un message sur la `CONQUESTS_QUEUE`, il arrête. S'il ne recoit rien pendant 5min, il avoue la défaite. Pour suivre les message sur la Amazon SQS Console, c'est [ici](https://console.aws.amazon.com/sqs/home?region=eu-west-1#).

mavn-zombies
---

Ultime étape: Nous allons utiliser les zombies pour télécharger Internet. Le protocole est simple également: Le maître envoie une URL sur la `COMMANDS_QUEUE`. Le zombie la lis, la télécharge, stocke le fichier sur S3, et renvoie la liste des URL contenus dans le fichier téléchargé, au format "text/uri-list", sur la `CONQUESTS_QUEUE`. Le maître lis les URLs de la `CONQUESTS_QUEUE`, utilise un Set pour les compter et retirer les doublons, et demande sur la `COMMANDS_QUEUE` à télécharger les URLs pas encore traitées, dans la limite de MAX_COUNT. Lorsque le maître termine, les zombies meurent après 5min d'attente.