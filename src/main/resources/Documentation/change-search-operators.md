Search Operators
================

**in_@PLUGIN@:<change>**

: Changes that are listed as "Depends-on" change dependencies defined in the comments of the provided change.

**has_@PLUGIN@:<query>**

: Changes that have "Depends-on" change dependencies defined in their comments which match the given sub query.

**has:a_@PLUGIN@**

: Changes that have at least one "Depends-on" change dependencies defined in their comments.

### Operational Notes:

To use any operator of @PLUGIN@ plugin, change operator aliasing is needed since query parser
cannot parse dash(-) in an operator.
