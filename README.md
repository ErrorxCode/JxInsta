<p align="center"><img alt="JxInsta" height="300" src="/JxInsta.png"></p>
<p align="center">
  <img src="https://img.shields.io/github/license/ErrorxCode/JxInsta?style=for-the-badge">
  <img src="https://img.shields.io/github/stars/ErrorxCode/JxInsta?style=for-the-badge">
  <img src="https://img.shields.io/github/issues/ErrorxCode/JxInsta?color=red&style=for-the-badge">
  <img src="https://img.shields.io/github/forks/ErrorxCode/JxInsta?color=teal&style=for-the-badge">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Author-Rahil-cyan?style=flat-square">
  <img src="https://img.shields.io/badge/Open%20Source-Yes-cyan?style=flat-square">
  <img src="https://img.shields.io/badge/Written%20In-Java-cyan?style=flat-square">
</p>


# JxInsta
A java library of Instagram private web API (*may include mobile api in future*).  This library is built as a replacement for **instagram4j** since it was too old fashioned and was facing a challenge problem due to a similar header population problem among it's users. This project is lead by **[EasyInsta](https://github.com/ErrorxCode/EasyInsta)** and is part of it.

## Features

-   Lightweight and Easy 2 use, Object-oriented
-   No need API token
-   Supports  **Sending messages**
-   Supports  **Getting/fetching messages**
-   Supports  **Deleting message**
-   Supports  **_Realtime direct messages listener_** (Pending)
-   Supports  **Login using cache/saving sessions**
-   Supports  **Posting (Picture)**
-   Supports  **Adding stories (Photo)**
-   Supports  **Following/Unfollowing**
-   Supports  ***Acception/Ignoring follow request*** (Pending)
-   Supports  **Scrapping followings and followers**
-   Supports  **Getting profile data**
-   Supports  **Liking/commenting on post**
-   Supports  **Fetching feeds/timeline post**
-   Supports  **Downloading posts and pfp**

## Implimentation
Due to the outage of jitpack, the library cannot published. Therfore, you have to locally implement it by puting the .jar file in the lib directory.

#### Step 1 : Download the `.jar` from the release section and put it in `lib` folder of your project.

#### Step 2 : declare the dependency
*For gradle*

```groovy
dependencies {
    implementation files('libs/JxInsta-v1.0-beta.jar')
    implementation group: 'com.squareup.okhttp3', name: 'okhttp', version: '5.0.0-alpha.11' //skip if already implemented
}
```

*For maven*

```XML
<dependency>
    <groupId>com.sample</groupId>
    <artifactId>jxinsta</artifactId>
    <version>v1.0-beta-2</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/JxInsta-v1.0-beta-2.jar</systemPath>
</dependency>

<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>5.0.0-alpha.11</version>
</dependency>

```

You can search on Google for more info on including local dependencies.

## Acknowledgements

-   [Instagram usage limits](https://www.linkedin.com/pulse/stay-within-boundaries-complete-breakdown-instagrams-cmscc/)
- [Instagram daily limit](https://socialpros.co/instagram-daily-limits/#:~:text=Instagram's%20Daily%20Limits%20%E2%80%93%20Like,than%2030%20likes%20per%20hour)
-   [API Policies](https://developers.facebook.com/devpolicy/)
-   [About Instagram checkpoints and challenges](/Instagram+checkpoints.md)

## Its easy :)

```java
JxInsta insta = new JxInsta("username", "password", JxInsta.LoginType.WEB_AUTHENTICATION);  
insta.uploadStory(new File("photos/story-24.png"));  
var profile = insta.getProfile("username").;
...
```


## Documentation

[](https://github.com/ErrorxCode/EasyInsta#documentation)

~~Javadoc~~ (Not needed)

[User guide](https://github.com/ErrorxCode/JxInsta/wiki)


## FAQ

#### [Q.1] Can we use this library to make bots?

**Answer.** Yes. But Instagram doesn't allow them to make bots with their official graph APIs. Although this is not the official API, you should follow the usage limits to prevent detection.

#### [Q.2] Can we download stories or posts using this library?

**Answer.** Yes, and that too without login

#### [Q.3] Does the use of this library require any tokens or other keys?

**Answer.** No. You only need to have the username and password of the account. You can also log in using cookies and bearer tokens.

#### [Q.4] In Android, can we use Webview to log in?

**Answer.**  Yes, check  [this](https://github.com/ErrorxCode/JxInsta/wiki/Android-users#using-webview-for-login)  example on how to use that


## Contributing

Contributions are always welcome! There is a lot of scope for contribution in this library.

Please refer to  [Contribution guide](https://github.com/ErrorxCode/JxInsta/blob/main/CONTRIBUTING.md). Also, see the  [code of conduct](https://github.com/ErrorxCode/JxInsta/blob/main/CODE_OF_CONDUCT.md).
Please see [Todo. md](https://github.com/ErrorxCode/JxInsta/blob/main/Todo.md) to see what features are pending and what things you can add on your end.
## Support

The fastest channel to contact me is **Instagram**, just DM me and I'll reply to you within 24 hours. My Instagram : [x0.rahil](https://instagram.com/x0.rahil)

You can show your support by giving a ⭐.
