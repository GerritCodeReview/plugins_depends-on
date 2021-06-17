@PLUGIN@
========

"Depends-on:" tag provides a way to mark a change dependent on other
change(s). To mark a change dependent, post a comment with the list
of dependencies under a change in the Gerrit page (not an inline diff
comment) with the format "Depends-on: xxx yyy" where xxx and yyy are
two changes. The "Depends-on:" tag is case sensitive.

Example:
    Depends-on: 123 124
The above example will designate the change as depending on 2 other changes,
123 and 124

The @PLUGIN@ plugin propagates dependencies added by "Depends-on:" tag to
changes after change propagation. It treats latest "Depends-on:" tag as
valid dependencies and ignores older tags. If latest tag doesn't have any
changes that means there is no dependency and doesn't propagate anything.
