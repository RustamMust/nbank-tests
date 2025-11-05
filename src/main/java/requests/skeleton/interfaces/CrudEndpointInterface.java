package requests.skeleton.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(long id);
    Object get();
    Object put(BaseModel model);
    Object delete(long id);
}
