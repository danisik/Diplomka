rs.initiate();
use patents;
db.createCollection("patents", {});
use local
db.adminCommand({replSetResizeOplog: 1, size: Double(50000)})
quit();
