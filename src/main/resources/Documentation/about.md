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

The @PLUGIN@ plugin propagates dependencies added by a "Depends-on:" tag to
changes after change propagation. It treats changes listed in the latest
"Depends-on:" tag as valid dependencies and ignores older tags. If latest tag
doesn't have any changes, it means there is no dependency and no dependencies
will be propagated.
