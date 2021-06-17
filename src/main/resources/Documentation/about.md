@PLUGIN@
========

This plugin provides a way to mark a change dependent on other change(s). To
mark a change dependent, post a comment with the list of dependencies under a
change in the Gerrit page (not an inline diff comment) in a single line with
the format `Depends-on: c1 c2 c3 ....` where c1, c2 and c3 are gerrit change
numbers. Any number of changes can be provided after the `Depends-on:` tag.
The `Depends-on:` tag is case-sensitive. Only changes listed in the most
recent `Depends-on:` tag are considered as valid dependencies and older tags
are ignored. To remove existing dependencies, a `Depends-on:` tag with no
changes must be added.

PROPAGATION
-----------

When a change is propagated, the @PLUGIN@ plugin adds a `Depends-on:` tag
to the propagated change. `Depends-on:` created via change propagation have
Change-Ids rather than actual change numbers. This plugin doesn't automatically
propagate dependencies as there is no generic way to determine what the right
destination branches are. A new `Depends-on:` tag can be added manually by
updating the Change-Ids to the right change numbers if they resolve to changes
destined for the desired branches.

EXAMPLES
--------

Adding below as a change comment makes the change dependent on two other
changes, 123 and 124.
```
Depends-on: 123 124
```

When the change is propagated, following tag is added on the destination
change, where *Ibd61365f87a4d7fbb5d62ffbe4f563f675e000c5* and
*I9a4b8b1499422464310cd6fd54e01fe0d1cf6714* are the Change-Ids of 123 and 124
respectively.
```
Depends-on: Ibd61365f87a4d7fbb5d62ffbe4f563f675e000c5 I9a4b8b1499422464310cd6fd54e01fe0d1cf6714
```

Adding below as a change comment makes the change not dependent on any other
changes. When such a change is propagated, no Depends-on tag is added to the
propagated change.
```
Depends-on:
```
