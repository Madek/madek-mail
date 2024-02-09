(ns madek.mail.utils.logging
  (:require
   [taoensso.timbre :as timbre :refer [debug info]]))

(def LOGGING_CONFIG
  {:min-level [[#{; examples:
                  ; "madek.mail.*"
                  "madek.mail.*"} :debug]
               [#{"com.zaxxer.hikari.*"
                  "madek.*"} :info]
               [#{"*"} :warn]]
   :log-level nil})

(defn init
  ([] (init LOGGING_CONFIG))
  ([logging-config]
   (info "initializing logging " logging-config)
   (timbre/merge-config! logging-config)
   (info "initialized logging " (pr-str timbre/*config*))))
