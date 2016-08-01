Interest Predictor
==================

A machine learning program to predict user's interest based on 3 social media sources:

    - LinkedIn
    - Facebook
    - Twitter

The interests are scoped to 20 interest:

    - reading
    - investing
    - advertising
    - social media
    - design
    - branding
    - photography
    - web development
    - web design
    - philosophy
    - business development
    - sailing
    - software development
    - skiing
    - marketing
    - internet marketing
    - online marketing
    - advertising
    - social networking
    - sailing
    - branding

The system includes topic modelling using LDA (Latent Dirichlet Allocation) and SVM (Support Vector Machine) as its classifier.

Steps to import and run project:

1. Import the root directory of this repository into your own Eclipse IDE
2. If the external JARs dependencies are not resolved automatically, add the following JARs as external JARs:
    - javax.json-1.0.4.jar
    - javax.json-api-1.0.jar
    - Jama-1.0.3.jar
    - jsoup-1.8.3.jar
    - libsvm.jar
3. Clone `InterestPredictorData` repository and copy the content into the root directory of this repository
4. Run the code from eclipse
