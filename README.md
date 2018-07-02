# Handwritten digit recognizer
Machine learning project to recognize handwritten digits.

Application serving at: https://handwriting-19.appspot.com/

## Architecture
The neural network has 3 layers: 1 input, 1 output and 1 hidden layer. The hidden layer contains 300 units, the input layer has units for each pixel and we have ten output units, one for each digit.

![architecture](https://github.com/kavai77/handwriting/blob/master/src/main/webapp/Neural_network.png)

## Training Algorithm
The training was performed by me using Octave programming language and the backpropagation algorithm.

The cost function is the generalization of logistic regression. The cost function includes regularization in order to keep the weights small and thus generalize the model more.

With backpropagation we are able to calculate the partial derivative of the cost function on every weight. When the optimalization algorithm is fed with both the cost function and its derivatives, it can minimize the cost function using gradient descent.

Both the backpropagation and the cost function is my implementation in Octave. The optimalization algorithm (fmincg) is based upon Andrew Ng's Coursera class: Machine Learning.

## Prediction Algorithm
The prediction algorithm is a Java service hosted in Google Cloud Platform. 

After having the weights for each unit, the prediction is an implementation of forward propagation 
which is basically only matrix multiplication and the sigmoid function applied. 

I did not use any ML library for this, the Apache Commons Math was enough.

## Training set
The neural network was trained using the [MNIST database](http://yann.lecun.com/exdb/mnist/) 
using 60000 examples. The training set is visualised [here](https://raw.githubusercontent.com/kavai77/handwriting/master/src/main/webapp/mnist-all.png)