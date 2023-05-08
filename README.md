# Instagram

Lasse dir alle deine Instagram-Statistiken auf einem Blick ausgegeben. In diesem Projekt wird keine offizielle Instagram API verwendet. Die api wurde reverse engineered. Das Projekt gilt nur zu Lern- und Testzwecken. Es kann jederzeit die FUnktionsweise Seitens Instagram verändert werden, sodass das Projekt nicht mehr funktioniert.

Einrichtung:
``` 
mvn install
```

Features:

- Logger

- Threads

- Authentifizierung
    - Session 🍪
    - Credentials📋(Benutztername und Passwort)
      - wenn session nicht funktioniert werden credentials verwendet

- Ausgabe
    - Anzahl
      - likes👍
      - comments⌨️
    - Anzahl und Person
      - Follower 
      - Following
      - Posts🖼️
      - NotFollowingYou
      - youFollowingNot
      - mutual
      - openFriendRequestIn
    
    - Sortierung
      - Posts(meiste⬆️ oder wenigste⬇️ likes oder comments)


