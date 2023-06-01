Search Attributes
=================

Change Query Output
-------------------
It is possible to add a dependsOns section to the query output of changes
using the @PLUGIN@ plugin switches. The following switches are available:

**\-\-@PLUGIN@\-\-all**

This switch is meant to be used to show all depends-ons from the latest
Depends-on comment on the change. The switch output order matches the
order from the change comment.

When a change has one or more depends-on associated with it, query output will
have a "dependsOns" section under the plugins section like below:

```
  $ ssh -x -p 29418 example.com gerrit query --@PLUGIN@--all \
        --format JSON change:144193 | head -1 | json_pp
  {
     "id" : "Ifc577c2660c26220c39df57627ca1053c3f2067c",
     ...
     "plugins" : [
        {
           "name" : "@PLUGIN@",
           "dependsOns" : [
              {
                "changeNumber": 732
              },
              {
                "changeNumber": 733
              },
              {
                "unresolved": "Ieace383c14de79bf202c85063d5a46a0580724dd"
              },
              {
                "changeNumber": 734
              }
           ],
           "name": "depends-on"
        }
     ]
  }
```
