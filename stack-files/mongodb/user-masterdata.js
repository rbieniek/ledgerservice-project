db.getSiblingDB("masterdata").createUser({
  user: "masterdata",
  pwd: "master123",
  roles: [
      "dbOwner"
  ]
});
