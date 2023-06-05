(ns clojure-api.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(def store (atom {}))
;exemplo
; {id {tarefa-id tarefa-nome tarefa-status} })

(defn lista-tarefas [request]
  {:status 200 :body @store})

(defn criar-tarefa-mapa [uuid nome status]
  {:id uuid :nome nome :status status})

(defn criar-tarefa [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        tarefa (criar-tarefa-mapa uuid nome status)]
    (swap! store assoc uuid tarefa)
    {:status 200 :body {:mensagem "Tarefa criada com sucesso "
                        :tarefa tarefa}}))

(defn hello-function [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(def routes (route/expand-routes
              #{["/hello" :get hello-function :route-name :hello-world]
                ["/tarefa" :post criar-tarefa :route-name :criar-tarefa]
                ["/tarefa" :get lista-tarefas :route-name :lista-tarefas]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))
(defn start-server []
  (reset! server (http/start (http/create-server service-map))))



(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(println (test-request :get "/hello?name=Milena"))
(println (test-request :post "/tarefa?name=aprender%20clojure&status=pendente"))
(println (test-request :post "/tarefa?name=correr&status=pendente"))

(println "Listando todas as tarefas")
(println (test-request :get "/tarefa"))

