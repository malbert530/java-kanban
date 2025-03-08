package manager;

import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest {

    @BeforeEach
    public void initManager() {
        manager = getTaskManager();
    }

    @Override
    TaskManager getTaskManager() {
        return Managers.getDefault();
    }
}