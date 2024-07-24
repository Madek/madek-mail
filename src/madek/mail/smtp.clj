(ns madek.mail.smtp
  (:require
   [clj-yaml.core :as yaml]
   [clojure.core.memoize :as memoize]
   [environ.core :refer [env]]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [madek.mail.db.core :refer [get-ds]]
   [madek.mail.utils.cli :refer [long-opt-for-key]]
   [madek.mail.utils.core :refer [presence]]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
   [taoensso.timbre :refer [info]]))

;;; state ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def pause-seconds (atom nil))
(def retries-seconds (atom nil))

;;; cli ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mail-send-pause-secs-key :mail-send-pause-secs)
(def send-pause-secs-opt
  [nil (long-opt-for-key mail-send-pause-secs-key)
   :default (-> (or (some-> mail-send-pause-secs-key env presence)
                    "1")
                Integer/parseInt)
   :parse-fn #(Integer/parseInt %)
   :validate [#(and (< 0 % 1000) (int? %))
              #(str "must be an int between 0 and 100")]])

(def mail-retries-seconds-key :mail-retries-seconds)
(def retries-in-seconds-opt
  [nil (long-opt-for-key mail-retries-seconds-key)
   :default (or (some-> retries-in-seconds-opt
                        env presence yaml/parse-string)
                [5,10,30,60,300,3600,18000])
   :parse-fn yaml/parse-string
   :validate [#(and (seq %)
                    (every? int? %))
              #("YAML parseable, non empty array of integers")]])

(def cli-options
  [send-pause-secs-opt
   retries-in-seconds-opt])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn db-settings-uncached []
  (-> (sql/select :*)
      (sql/from :smtp_settings)
      sql-format
      (->> (jdbc-query (get-ds)))
      first))

(def db-settings
  (memoize/ttl db-settings-uncached :ttl/threshold 1000))

(defn host-address
  []
  (:host_address (db-settings)))

(defn port
  []
  (:port (db-settings)))

(defn domain
  []
  (:domain (db-settings)))

(defn sender-address
  []
  (:sender_address (db-settings)))

(defn enable-starttls-auto
  []
  (:enable_starttls_auto (db-settings)))

(defn username []
  (:username (db-settings)))

(defn password []
  (:password (db-settings)))

(defn is-enabled []
  (:is_enabled (db-settings)))

(defn all []
  {:host-address (host-address)
   :port (port)
   :username (username)
   :password (password)
   :domain (domain)
   :sender-address (sender-address)
   :enable-starttls-auto (enable-starttls-auto)
   :is-enabled (is-enabled)
   :pause-seconds @pause-seconds
   :retries-seconds @retries-seconds})

;;; init ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init [opts]
  (info "initializing SMTP settings ...")
  (reset! pause-seconds
          (get opts mail-send-pause-secs-key))
  (reset! retries-seconds
          (get opts mail-retries-seconds-key))
  (info "initialized SMTP settings " (all)))
