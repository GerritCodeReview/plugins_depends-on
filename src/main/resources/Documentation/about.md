@PLUGIN@
========

"Depends-on:" tag provides a way to mark a change dependent on other
change(s). To mark a change dependent, post a comment with the list
of dependencies under a change in the Gerrit page (not an inline diff
comment) in a single line with the format "Depends-on: c1 c2 c3 ...."
where c1, c2 and c3 are gerrit change numbers. Any number of changes
can be provided after the "Depends-on:" tag. The "Depends-on:" tag is
case-sensitive.

Example:
    Depends-on: 123 124
The above example will designate the change as depending on 2 other changes,
123 and 124

When a change is propagated, the @PLUGIN@ plugin adds a "Depends-on:" tag
to the propagated change. It treats changes listed in the latest "Depends-on:"
tag as valid dependencies and ignores older tags. To remove existing
dependencies, a "Depends-on:" tag with no changes must be added.

Example:
    Depends-on:
In the above example, the change is not dependent on any other changes and
when the change is propagated, no depends-on tag is added to the propagated
change.
