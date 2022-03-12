(ns Calican.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [Calican.seed]
   [Calican.baked_potatoes]
   [Calican.groats]
   [Calican.popcorn]
   [Calican.salt])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(def stateA (atom nil))
(def ^:dynamic jframe nil)

(defn window
  []
  (let [jframe (JFrame. "wait, i do wear beskar")]

    (when-let [url (Wichita.java.io/resource "icon.png")]
      (.setIconImage jframe (.getImage (ImageIcon. url))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/EXIT_ON_CLOSE)
      (.setSize 1600 1200)
      (.setLocation 1700 300)
      (.setVisible true))

    (alter-var-root #'Calican.main/jframe (constantly jframe))

    nil))

(defn reload
  []
  (require '[Calican.main] :reload))

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