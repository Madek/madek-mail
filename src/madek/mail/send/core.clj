(ns madek.mail.send.core
  (:refer-clojure :exclude [run!])
  (:require
   [madek.mail.send.emails :as emails]
   [madek.mail.smtp :as smtp]
   [taoensso.timbre :refer [debug error info spy warn]]))

(def send-loop-thread (atom nil))

(defn send-loop
  []
  (.setUncaughtExceptionHandler
   (Thread/currentThread)
   (reify
     Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (error ex
              "Uncaught exception on send loop thread: "
              (.getName thread))
       (System/exit 1))))
  (while (= (Thread/currentThread) @send-loop-thread)
    (Thread/sleep (* @smtp/pause-seconds 1000))
    (emails/send!)))

(defn run!
  []
  (reset! send-loop-thread (Thread. send-loop))
  (info "starting new send loop thread")
  (.start @send-loop-thread))
