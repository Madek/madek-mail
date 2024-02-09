(ns madek.mail.utils.exception)

(defn get-cause [e]
  "Deprecated"
  (try (if (instance? java.sql.BatchUpdateException e)
         (if-let [n (.getNextException e)]
           (get-cause n) e)
         (if-let [c (.getCause e)]
           (get-cause c) e))
       (catch Throwable _ e)))
