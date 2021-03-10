Unofficial Clubhouse app for Android. Inspired by [this](https://github.com/stypr/clubhouse-py).

Not to be taken seriously.

**This app is NOT available in Google Play. The only right place to download it is here in the releases section.**

**Any issues about audio quality will be closed immediately, so please don't bother posting those.**

# FAQ
### What works?
* Login
* Registration *should* work, but I suggest that you better use an iOS device to register
* Seeing the list of rooms however the server recommends them
* Joining rooms from said list and by direct links
* Listening and speaking
* Raising hand (asking to speak)
* Accepting when a moderator allows you to speak
* Real-time updates in to the participant list
* Profiles
* Following and unfollowing people
* Followers/following lists
* Updating your "bio"
* Uploading a profile picture
* Changing your name (but the official app says you can only do this once — not sure if there's a limitation on the server side)
* Notifications

The rest isn't implemented. In particular you can't create and moderate rooms. There's probably a hundred bugs in the existing functionality too.

### Java? In %year%?! Why not Kotlin? Why are there no jetpack libraries? System fragments?!?! Are you out of your mind?
Maybe I am. I use tools I'm most familiar with, not ones that are trendy at the particular day I'm starting the project.

### The design...
...sucks. Yes it does. See, this isn't meant to be a real product. I hastily put this together in 1.5 days. Alsmost like on a hackathon. This is more of a proof of concept, a stopgap measure before Clubhouse releases their official Android app that I'm sure they're making right now. It doesn't make sense to spend all this effort on something that's going to be obsolete in less than a year.

If you want to see a project where I do take UI/UX seriously and am obsessed over every little detail, go check out [Smithereen](https://github.com/grishka/Smithereen), the decentralized social media server.

### Will this get my Clubhouse account banned?
The probability of that happening is not zero.

### Why did you make this?
I saw that project with the Clubhouse API reverse engineersed and thought to myself *why not*. The only thing I had to reverse engineer myself was the PubNub part, but a pirated copy of Charles made a quick job of that.

### How do I build this?
Import into Android Studio and click "run". Or, there's an apk you can install in the releases section.
