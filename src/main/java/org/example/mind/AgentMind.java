package org.example.mind;

import br.unicamp.cst.core.entities.*;
import org.example.environment.RiverRaidEnv;
import org.example.mind.codelets.event_cat_learner.EventCategoryLearnerCodelet;
import org.example.mind.codelets.object_cat_learner.ObjectCategoryLearnerCodelet;
import org.example.mind.codelets.object_proposer.ObjectProposerCodelet;
import org.example.mind.codelets.RAWDataBufferizerCodelet;
import org.example.mind.codelets.objects_bufferizer.ObjectsBufferizerCodelet;

import javax.swing.*;
import java.io.IOException;


public class AgentMind extends Mind {
    public AgentMind(RiverRaidEnv env,
                     JLabel rawDataBufferImgJLabel,
                     JLabel objectsImgJLabel,
                     JLabel mergedObjectsImgJLabel,
                     JLabel categoriesImgJLabel) throws IOException {
        super();

        Memory rawDataBufferMO;
        Memory detectedFragmentsMO;
        Memory detectedObjectsMO;
        Memory fragmentCategoriesMO;
        Memory objectCategoriesMO;
        Memory objectsBufferMO;
        Memory eventCategoriesMO;

        createMemoryGroup("EpisodeTrackerMemoryGroup");
        createCodeletGroup("EpisodeTrackerCodeletGroup");

        rawDataBufferMO = createMemoryObject("RAW_DATA_BUFFER", "");
        detectedFragmentsMO = createMemoryObject("DETECTED_FRAGMENTS", "");
        detectedObjectsMO = createMemoryObject("DETECTED_OBJECTS", "");
        fragmentCategoriesMO = createMemoryObject("FRAGMENT_CATEGORIES", "");
        objectCategoriesMO = createMemoryObject("OBJECT_CATEGORIES", "");
        objectsBufferMO = createMemoryObject("OBJECTS_BUFFER", "");
        eventCategoriesMO = createMemoryObject("EVENT_CATEGORIES", "");

        registerMemory(rawDataBufferMO, "EpisodeTrackerMemoryGroup");
        registerMemory(detectedFragmentsMO, "EpisodeTrackerMemoryGroup");
        registerMemory(detectedObjectsMO, "EpisodeTrackerMemoryGroup");
        registerMemory(fragmentCategoriesMO, "EpisodeTrackerMemoryGroup");
        registerMemory(objectCategoriesMO, "EpisodeTrackerMemoryGroup");
        registerMemory(objectsBufferMO, "EpisodeTrackerMemoryGroup");
        registerMemory(eventCategoriesMO, "EpisodeTrackerMemoryGroup");

        Codelet rawDataBufferizerCodelet = new RAWDataBufferizerCodelet(env, rawDataBufferImgJLabel);
        rawDataBufferizerCodelet.addOutput(rawDataBufferMO);
        rawDataBufferizerCodelet.setName("RAWDataBufferizer");
        insertCodelet(rawDataBufferizerCodelet);

        Codelet objectProposerCodelet = new ObjectProposerCodelet(objectsImgJLabel, mergedObjectsImgJLabel, categoriesImgJLabel);
        objectProposerCodelet.addInput(rawDataBufferMO);
        objectProposerCodelet.addInput(fragmentCategoriesMO);
        objectProposerCodelet.addInput(objectCategoriesMO);
        objectProposerCodelet.addOutput(detectedFragmentsMO);
        objectProposerCodelet.addOutput(detectedObjectsMO);
        objectProposerCodelet.setIsMemoryObserver(true);
        rawDataBufferMO.addMemoryObserver(objectProposerCodelet);
        objectProposerCodelet.setName("ObjectProposer");
        insertCodelet(objectProposerCodelet);

        Codelet objectCategoryLearnerCodelet = new ObjectCategoryLearnerCodelet();
        objectCategoryLearnerCodelet.addInput(detectedFragmentsMO);
        objectCategoryLearnerCodelet.addOutput(fragmentCategoriesMO);
        objectCategoryLearnerCodelet.addOutput(objectCategoriesMO);
        objectCategoryLearnerCodelet.setIsMemoryObserver(true);
        detectedFragmentsMO.addMemoryObserver(objectCategoryLearnerCodelet);
        objectCategoryLearnerCodelet.setName("ObjectCategoryLearner");
        insertCodelet(objectCategoryLearnerCodelet);

        Codelet objectsBufferizerCodelet = new ObjectsBufferizerCodelet();
        objectsBufferizerCodelet.addInput(detectedObjectsMO);
        objectsBufferizerCodelet.addOutput(objectsBufferMO);
        objectsBufferizerCodelet.setIsMemoryObserver(true);
        detectedObjectsMO.addMemoryObserver(objectsBufferizerCodelet);
        objectsBufferizerCodelet.setName("ObjectsBufferizerCodelet");
        insertCodelet(objectsBufferizerCodelet);

        Codelet eventCategoryLearnerCodelet = new EventCategoryLearnerCodelet();
        eventCategoryLearnerCodelet.addInput(objectsBufferMO);
        eventCategoryLearnerCodelet.addOutput(eventCategoriesMO);
        eventCategoryLearnerCodelet.setIsMemoryObserver(true);
        objectsBufferMO.addMemoryObserver(eventCategoryLearnerCodelet);
        eventCategoryLearnerCodelet.setName("EventCategoryLearner");
        insertCodelet(eventCategoryLearnerCodelet);

        registerCodelet(rawDataBufferizerCodelet, "EpisodeTrackerCodeletGroup");
        registerCodelet(objectProposerCodelet, "EpisodeTrackerCodeletGroup");
        registerCodelet(objectCategoryLearnerCodelet, "EpisodeTrackerCodeletGroup");
        registerCodelet(objectsBufferizerCodelet, "EpisodeTrackerCodeletGroup");
        registerCodelet(eventCategoryLearnerCodelet, "EpisodeTrackerCodeletGroup");

        // Sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(100);

        // Start Cognitive Cycle
        start();

    }
}
