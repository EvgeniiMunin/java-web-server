package org.server;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.util.Collections;

public class OnnxModelRunner {

    private static final OrtEnvironment ENVIRONMENT = OrtEnvironment.getEnvironment();

    private OrtSession session;

    public OnnxModelRunner(String modelPath) throws OrtException {
        session = ENVIRONMENT.createSession(modelPath, new OrtSession.SessionOptions());
    }

    public OrtSession.Result runModel(String[][] bidders) throws OrtException {
        final OnnxTensor inputTensor = OnnxTensor.createTensor(ENVIRONMENT, bidders);
        return session.run(Collections.singletonMap("input", inputTensor));
    }
}
