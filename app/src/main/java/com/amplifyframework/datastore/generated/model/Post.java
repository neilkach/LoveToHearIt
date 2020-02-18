package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.annotations.HasMany;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Post type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Posts")
public final class Post implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField TITLE = field("title");
  public static final QueryField BLOG = field("postBlogId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String title;
  private final @ModelField(targetType="Blog") @BelongsTo(targetName = "postBlogId", type = Blog.class) Blog blog;
  private final @ModelField(targetType="Comment") @HasMany(associatedWith = "post", type = Comment.class) List<Comment> comments = null;
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public Blog getBlog() {
      return blog;
  }
  
  public List<Comment> getComments() {
      return comments;
  }
  
  private Post(String id, String title, Blog blog) {
    this.id = id;
    this.title = title;
    this.blog = blog;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Post post = (Post) obj;
      return ObjectsCompat.equals(getId(), post.getId()) &&
              ObjectsCompat.equals(getTitle(), post.getTitle()) &&
              ObjectsCompat.equals(getBlog(), post.getBlog());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getBlog())
      .toString()
      .hashCode();
  }
  
  public static TitleStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   **/
  public static Post justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Post(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      blog);
  }
  public interface TitleStep {
    BuildStep title(String title);
  }
  

  public interface BuildStep {
    Post build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep blog(Blog blog);
  }
  

  public static class Builder implements TitleStep, BuildStep {
    private String id;
    private String title;
    private Blog blog;
    @Override
     public Post build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Post(
          id,
          title,
          blog);
    }
    
    @Override
     public BuildStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep blog(Blog blog) {
        this.blog = blog;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     **/
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String title, Blog blog) {
      super.id(id);
      super.title(title)
        .blog(blog);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder blog(Blog blog) {
      return (CopyOfBuilder) super.blog(blog);
    }
  }
  
}
