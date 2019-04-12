
## Things to keep in mind

1. Never 'push' directly to 'develop' or 'master'. You can, however, push directly into the branch(es) that you've created.
2. If you want to put the code from your branch into develop or master, you must do a pull request.
3. You can change the name of your app at any time, but you need to make a pull request to do that.
4. I must be included in all pull requests.
5. Right now your project is named "My Application" and the string for that is in the `res/values/strings.xml` file. The Application has a package directory of `com.example.jacobgraves.myapplication` which will need to be changed in `app/manifests/AndroidManifest.xml`, the build.gradle in the app module, and the actual package folders in `java/*`.
Consider renaming it to `com.{group name}.{app name}` once you have your app name nailed down.
6. Any other questions may be directed at me either on canvas or via email: jacobgraves@suu.edu

---

Go ahead and explore the repository. You've already seen the **Source** page, but check out the **Commits**, **Branches**, and **Settings** pages.

---

## Clone a repository

Use these steps to clone from SourceTree, our client for using the repository command-line free. Cloning allows you to work on your files locally. If you don't yet have SourceTree, [download and install first](https://www.sourcetreeapp.com/). If you prefer to clone from the command line, see [Clone a repository](https://confluence.atlassian.com/x/4whODQ).

1. You’ll see the clone button under the **Source** heading. Click that button.
2. Now click **Check out in SourceTree**. You may need to create a SourceTree account or log in.
3. When you see the **Clone New** dialog in SourceTree, update the destination path and name if you’d like to and then click **Clone**.
4. Open the directory you just created to see your repository’s files.

Now that you're more familiar with your Bitbucket repository, go ahead and add a new file locally. You can [push your change back to Bitbucket with SourceTree](https://confluence.atlassian.com/x/iqyBMg), or you can [add, commit,](https://confluence.atlassian.com/x/8QhODQ) and [push from the command line](https://confluence.atlassian.com/x/NQ0zDQ).