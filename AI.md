# Utilisation de l'IA – TP 4 : Application Web RAG

## Vue d'ensemble

Pour cette partie du TP, j'ai utilisé Claude puis ChatGPT pour m'aider à résoudre certaines difficultés rencontrées lors de l'intégration du RAG dans une application Web Jakarta EE avec LangChain4j.

J'ai réalisé les modifications dans IntelliJ, compilé et déployé l'application sur Payara, puis effectué les tests. Lorsque je rencontrais un problème, je transmettais les erreurs, les réponses obtenues ou les logs à l'IA pour comprendre leur origine.

Voici les situations dans lesquelles l'IA m'a été particulièrement utile.

---

## 1. Erreur HTTP 500 lors de l'appel à Gemini

**Problème rencontré :** l'application se déployait correctement sur Payara, mais lorsque j'envoyais une question, elle affichait une erreur HTTP 500. Un appel direct à Gemini depuis PowerShell expirait également.

**Pourquoi j'ai utilisé l'IA :** je voulais comprendre si le problème venait de l'application Web, du serveur Payara ou de la communication avec Gemini.

**Prompt utilisé :** j'ai transmis l'erreur `GenAiIOException: Failed to execute HTTP request`, puis les résultats des tests PowerShell. J'ai ensuite demandé : « je crois que le probleme vient de gemini-3.8-flash on utilise le lite ».

**Réponse de l'IA :** l'IA m'a proposé de tester directement l'API Gemini pour isoler le problème. Elle m'a ensuite indiqué comment remplacer `gemini-3.8-flash` par `gemini-3.1-flash-lite` et redéployer l'application.

**Ce que j'ai appris :** j'ai compris qu'une application peut se compiler et se déployer correctement tout en rencontrant des problèmes lors d'un appel à une API externe. J'ai également appris à tester séparément les différents composants pour localiser une erreur. Après le changement de modèle, l'application a fonctionné, même si la cause exacte du problème initial n'a pas été confirmée.

---

## 2. Améliorer la récupération des informations dans les PDF

**Problème rencontré :** après avoir intégré les deux PDF dans le système RAG, j'ai posé une question sur une recette du livre NEFF. Le modèle ne retrouvait pas la quantité de blanc de poulet, alors que cette information était présente dans le document.

**Pourquoi j'ai utilisé l'IA :** je voulais comprendre pourquoi le RAG ne récupérait pas la bonne information et comment améliorer la recherche documentaire.

**Prompt utilisé :** j'ai transmis la conversation dans laquelle le modèle indiquait que les extraits fournis ne contenaient pas la recette ni la quantité recherchée.

**Réponse de l'IA :** l'IA m'a expliqué que le problème pouvait venir du découpage des documents et du nombre de segments récupérés. Elle m'a proposé d'utiliser `DocumentSplitters.recursive(600, 100)` pour créer des segments avec chevauchement et `.maxResults(4)` pour récupérer davantage d'extraits.

**Ce que j'ai appris :** j'ai compris que le RAG ne transmet pas tout le PDF au modèle, mais seulement les passages sélectionnés par le retriever. Une information peut donc exister dans le document sans apparaître dans le contexte fourni au LLM. J'ai également compris l'importance de la taille des segments et du chevauchement pour conserver les informations liées. Après les modifications, le modèle a correctement répondu **400 g**.

---

## 3. Routage intelligent entre plusieurs documents

**Problème rencontré :** au départ, l'application recherchait dans les deux PDF pour chaque question, même lorsqu'un seul document était pertinent.

**Pourquoi j'ai utilisé l'IA :** je voulais comprendre comment sélectionner automatiquement le document adapté à la question et vérifier ce choix dans les logs.

**Prompt utilisé :** pendant les tests, j'ai transmis à l'IA les traces de Payara, notamment `RAG - PDF consulte : rag.pdf` et `RAG - PDF consulte : autre.pdf`.

**Réponse de l'IA :** l'IA m'a proposé d'utiliser `LanguageModelQueryRouter` avec une description de chaque source. Elle m'a également conseillé d'ajouter des traces dans les retrievers pour identifier le PDF consulté.

**Ce que j'ai appris :** j'ai compris la différence entre le routeur, qui choisit la source pertinente, et le retriever, qui recherche les passages dans cette source. J'ai également appris à utiliser les logs pour vérifier que l'application consulte le bon document selon la question.

---

## 4. Reformulation des questions avec l'historique de conversation

**Problème rencontré :** certaines questions de suivi, comme « Et est-ce que ça modifie ses paramètres ? », ne contenaient pas suffisamment d'informations pour effectuer une recherche documentaire correcte sans connaître la conversation précédente.

**Pourquoi j'ai utilisé l'IA :** je voulais comprendre comment utiliser l'historique de conversation pour améliorer la recherche dans les PDF.

**Prompt utilisé :** j'ai transmis à l'IA les résultats du test ainsi que les logs montrant la question reformulée et le document consulté.

**Réponse de l'IA :** l'IA m'a proposé d'intégrer `CompressingQueryTransformer` dans le `RetrievalAugmentor` afin de reformuler la question en utilisant l'historique avant de lancer la recherche et le routage.

**Ce que j'ai appris :** j'ai compris que les questions de suivi peuvent être ambiguës lorsqu'elles sont utilisées seules pour rechercher des documents. Le QueryTransformer permet de les rendre plus explicites en utilisant le contexte de la conversation. Dans mes tests, la question sur les paramètres a été reformulée en une question sur le fine-tuning, puis le routeur a sélectionné le PDF du cours.

---

## Bilan

L'IA m'a principalement aidé à diagnostiquer les erreurs de communication avec Gemini et à comprendre comment améliorer le fonctionnement du RAG dans une application Web.

J'ai notamment approfondi mes connaissances sur le découpage des documents, la récupération des passages pertinents, le routage entre plusieurs sources et la reformulation des questions à partir de l'historique.

J'ai ensuite appliqué les modifications dans mon projet et vérifié leur fonctionnement à travers les tests de l'application et les logs de Payara.