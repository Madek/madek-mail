(ns madek.mail.utils.core)

(defn presence [v]
  "Returns nil if v is a blank string or if v is an empty collection.
  Returns v otherwise."
  (cond
    (string? v) (if (clojure.string/blank? v) nil v)
    (coll? v) (if (empty? v) nil v)
    :else v))
