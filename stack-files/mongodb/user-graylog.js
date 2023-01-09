db.getSiblingDB("graylog").createUser({
  user: "graylog",
  pwd: "gray123",
  roles: [
      "dbOwner"
  ]
});
