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

### Execution Notes:

For the sample use case, we have created a change with Id 1. This change depends on another change with Id 2.
To create this dependency use the below command :
```
ssh -p 29418 user@gerrit.example.com gerrit review --message \'"Depends-on: 2"\' 1,2
```

**in_depends-on:**

```
$ ssh -p 29418 user@gerrit.example.com gerrit query "independson:1" --format=JSON

{
  "project": "my-repo",
  "branch": "master",
  "id": "I556b2add7ab4b4209f710ebaf886a58282b64c55",
  "number": 2,
  "subject": "change 2",
  "owner": {
    "name": "Administrator",
    "email": "admin@example.com",
    "username": "admin"
  },
  "url": "http://192.168.1.3:8080/c/my-repo/+/21",
  "hashtags": [],
  "createdOn": 1715754415,
  "lastUpdated": 1715754418,
  "open": true,
  "status": "NEW"
}
{
  "type": "stats",
  "rowCount": 1,
  "runTimeMilliseconds": 37,
  "moreChanges": false
}
```

**has_depends-on:**

```
$ ssh -p 29418 user@gerrit.example.com gerrit query hasdependson:{change:2} --format=JSON

{
  "project": "my-repo",
  "branch": "master",
  "id": "I2d4818047fa9c3105636cfde1db6c7975c7da4dc",
  "number": 1,
  "subject": "change 1",
  "owner": {
    "name": "Administrator",
    "email": "admin@example.com",
    "username": "admin"
  },
  "url": "http://192.168.1.3:8080/c/my-repo/+/2",
  "hashtags": [],
  "createdOn": 1715074377,
  "lastUpdated": 1715759836,
  "open": true,
  "status": "NEW"
}
{
  "type": "stats",
  "rowCount": 1,
  "runTimeMilliseconds": 151,
  "moreChanges": false
}
```

**has:a_depends-on**

```
$ ssh -p 29418 user@gerrit.example.com gerrit query  "change:1 has:a_depends-on" --format=JSON

{
  "project": "my-repo",
  "branch": "master",
  "id": "I2d4818047fa9c3105636cfde1db6c7975c7da4dc",
  "number": 1,
  "subject": "change 1",
  "owner": {
    "name": "Administrator",
    "email": "admin@example.com",
    "username": "admin"
  },
  "url": "http://192.168.1.3:8080/c/my-repo/+/2",
  "hashtags": [],
  "createdOn": 1715074377,
  "lastUpdated": 1715759836,
  "open": true,
  "status": "NEW"
}
{
  "type": "stats",
  "rowCount": 1,
  "runTimeMilliseconds": 35,
  "moreChanges": false
}
```
