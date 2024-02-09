(ns madek.mail.main
  (:gen-class)
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.tools.cli :as cli]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [logbug.thrown]
   [madek.mail.db.core :as db]
   [madek.mail.send.core :as send]
   [madek.mail.smtp :as smtp]
   [madek.mail.utils.exit :as exit]
   [madek.mail.utils.logging :as logging]
   [madek.mail.utils.nrepl :as nrepl]
   ; [madek.mail.utils.rdbms :as rdbms]
   [taoensso.timbre :refer [debug error info spy warn]]))

;; cli ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  (concat
   [["-h" "--help"]
    ["-d" "--dev-mode"]]
   exit/cli-options
   nrepl/cli-options
   db/cli-options
   smtp/cli-options))

(defn main-usage [options-summary & more]
  (->> ["Madek Mail"
        ""
        "usage: madek-mail [<opts>] "
        ""
        "Options:"
        options-summary
        ""
        ""
        (when more
          ["-------------------------------------------------------------------"
           (with-out-str (pprint more))
           "-------------------------------------------------------------------"])]
       flatten (clojure.string/join \newline)))

(defn helpnexit [summary args options]
  (println (main-usage summary {:args args :options options})))

;; run ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run [options]
  (catcher/snatch
   {:level :fatal
    :throwable Throwable
    :return-fn (fn [e] (System/exit -1))}
   (info 'madek.mail.main "initializing ...")
   (info "Effective startup options " options)
   ; ---------------------------------------------------------
   ; WIP switching to new db container; remove old rdbms later
   ; (rdbms/initialize (config/get-db-spec :api))
   (db/init options)
   ; ---------------------------------------------------------
   (nrepl/init options)
   (smtp/init options)
   (exit/init options)
   (send/run!)))

;; main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce args* (atom nil))

(defn main []
  (logging/init)
  (info "main args:" @args*)
  (let [args @args*
        {:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options :in-order true)
        options (into (sorted-map) options)
        cmd (some-> arguments first keyword)]
    (info {'options options 'cmd cmd})
    (cond
      (:help options) (println (main-usage summary {:args args :options options}))
      :else (case cmd
              :run (run options)
              (println (main-usage summary {:args args :options options}))))))

(defn -main [& args]
  ;(logbug.thrown/reset-ns-filter-regex #".*madek.*")
  (reset! args* args)
  (main))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; hot reload on require
(when @args* (main))

;### Debug ####################################################################
;(debug/debug-ns 'madek.api.utils.rdbms)
