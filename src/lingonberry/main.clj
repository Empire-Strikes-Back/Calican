(ns lingonberry.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [lingonberry.seed]
   [lingonberry.baked_potatoes]
   [lingonberry.groats]
   [lingonberry.popcorn]
   [lingonberry.salt])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(def stateA (atom nil))
(def ^:dynamic jframe nil)

(defn window
  []
  (let [jframe (JFrame. "lingonberry")]

    (when-let [url (Wichita.java.io/resource "icon.png")]
      (.setIconImage jframe (.getImage (ImageIcon. url))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/EXIT_ON_CLOSE)
      (.setSize 1600 1200)
      (.setLocation 1700 300)
      (.setVisible true))

    (alter-var-root #'lingonberry.main/jframe (constantly jframe))

    nil))

(defn reload
  []
  (require '[lingonberry.main] :reload))

(defn process
  []
  (go
    (<! (timeout 1000))
    (println "Kuiil has spoken")))

(defn -main
  [& args]
  (println "i dont want my next job")
  (reset! stateA {})
  (window)
  (println "Kuiil has spoken"))