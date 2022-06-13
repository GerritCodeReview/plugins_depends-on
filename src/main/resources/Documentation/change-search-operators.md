Search Operators
================

**in_@PLUGIN@:<change>**

: Returns "Depends-on" change dependencies specified in the comments of the provided change.

**has:a_@PLUGIN@**

: Returns changes that have "Depends-on" change dependencies defined in their comments (operator does not return changes with an empty "Depends-on").

**Operational Notes**:

To use any operator of @PLUGIN@ plugin, change operator aliasing is needed since query parser
cannot parse dash(-) in an operator.
