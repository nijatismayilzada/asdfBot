AsdfBot is developed as part of the 2nd-semester project of COMP34120 AI and Games course unit. The bot is implemented by asdf team, a group of 3 students; Ramin Jafarov, Kamil Orujzade, Nijat Ismayilzada.

Three different configured bots were implemented, which were customised for FollowerMK1, FollowerMK2 and FollowerMK3 respectively. To run the bots, navigate to the root folder of the project and execute following commands:

1. Enable RMI registration and run the GUI of the platform as instructed in the project overview:
		rmiregistry
		java -classpath poi-3.7-20101029.jar: -Djava.rmi.server.hostname=127.0.0.1 comp34120.ex2.Main

2. Compile java code: 
		javac -cp comp34120/ex2/:poi-3.7-20101029.jar:src/ src/*.java -d .

3. Run each java code below for the relative followers:
		java -Djava.rmi.server.hostname=127.0.0.1 AsdfLeaderMk1
		java -Djava.rmi.server.hostname=127.0.0.1 AsdfLeaderMk2
		java -Djava.rmi.server.hostname=127.0.0.1 AsdfLeaderMk3