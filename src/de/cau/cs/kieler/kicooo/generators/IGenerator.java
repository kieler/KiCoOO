package de.cau.cs.kieler.kicooo.generators;

import java.io.PrintStream;
import java.nio.file.Path;

import de.cau.cs.kieler.kicooo.model.State;

public interface IGenerator {

    public void createStaticFiles(Path outputFolder);

    public void createMainClass(State mainRootState, Path outputFolder);

    public void processRootState(State state, Path outputFolder);

}
