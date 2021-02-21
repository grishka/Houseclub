Unofficial Clubhouse app for Android forked and rewritten to Kotlin. Inspired by [this](https://github.com/stypr/clubhouse-py).

<img alt="Kotlin" src="https://img.shields.io/badge/kotlin-%230095D5.svg?&style=for-the-badge&logo=kotlin&logoColor=white"/>

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

The rest isn't implemented. In particular, there are no notifications, and you can't create and moderate rooms. There's probably a hundred bugs in the existing functionality too.

 ̶#̶#̶#̶ ̶J̶a̶v̶a̶?̶ ̶I̶n̶ ̶%̶y̶e̶a̶r̶%̶?̶!̶ ̶W̶h̶y̶ ̶n̶o̶t̶ ̶K̶o̶t̶l̶i̶n̶?̶ ̶W̶h̶y̶ ̶a̶r̶e̶ ̶t̶h̶e̶r̶e̶ ̶n̶o̶ ̶j̶e̶t̶p̶a̶c̶k̶ ̶l̶i̶b̶r̶a̶r̶i̶e̶s̶?̶ ̶S̶y̶s̶t̶e̶m̶ ̶f̶r̶a̶g̶m̶e̶n̶t̶s̶?̶!̶?̶!̶ ̶A̶r̶e̶ ̶y̶o̶u̶ ̶o̶u̶t̶ ̶o̶f̶ ̶y̶o̶u̶r̶ ̶m̶i̶n̶d̶?̶
No more java, ONLY Kotlin 

### The design...
...sucks. Yes it does. See, this isn't meant to be a real product. I hastily put this together in 1.5 days. Alsmost like on a hackathon. This is more of a proof of concept, a stopgap measure before Clubhouse releases their official Android app that I'm sure they're making right now. It doesn't make sense to spend all this effort on something that's going to be obsolete in less than a year.

If you want to see a project where I do take UI/UX seriously and am obsessed over every little detail, go check out [Smithereen](https://github.com/grishka/Smithereen), the decentralized social media server.

### Will this get my Clubhouse account banned?
The probability of that happening is not zero.

### Why did you make this?
I saw that project with the Clubhouse API reverse engineersed and thought to myself *why not*. The only thing I had to reverse engineer myself was the PubNub part, but a pirated copy of Charles made a quick job of that.

### How do I build this?
Import into Android Studio and click "run". Or, there's an apk you can install in the releases section.
