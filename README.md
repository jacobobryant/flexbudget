# FlexBudget

This is a simple CRUD app built with [Trident]. See [this article] for some
information about the implementation. If you'd like to read the code, a good
starting point is `backend/src/bud/backend/core.clj`. Currently deployed at
https://notjust.us. If you're looking around for a budgeting app, I recommend
it.

Although this is a Datomic Cloud Ion app, I have multiple apps deployed on the
same (solo) topology. This has a few implications:

 - I store ion configuration in a `bud-ion-config.edn` file which then gets
   merged into an `ion-config.edn` file in a parent project. The parent project
   then gets deployed, not this project.
 - There seems to be a bug with transitive `local/root` dependencies in
   `deps.edn`, so the `../shared` dependency in `backend/deps.edn` is commented
   out. I include it directly in the parent project instead.
 - Normally I would include Ion dependency conflicts in `backend/deps.edn`, but
   I've moved those to the parent project as well.

## License

Distributed under the [EPL v2.0]

Copyright &copy; 2019 [Jacob O'Bryant].

[Trident]: https://github.com/jacobobryant/trident
[this article]: https://jacobobryant.com/post/2019/ion/
[EPL v2.0]: https://github.com/jacobobryant/flexbudget/blob/master/LICENSE
[Jacob O'Bryant]: https://jacobobryant.com
