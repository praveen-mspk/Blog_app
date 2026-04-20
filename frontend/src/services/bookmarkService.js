const BOOKMARKS_KEY = 'bloghub_bookmarks';

export const getBookmarks = () => {
  try {
    const saved = localStorage.getItem(BOOKMARKS_KEY);
    return saved ? JSON.parse(saved) : [];
  } catch (error) {
    console.error("Error reading bookmarks", error);
    return [];
  }
};

export const toggleBookmark = (post) => {
  const bookmarks = getBookmarks();
  const index = bookmarks.findIndex(b => b.id === post.id);
  
  if (index >= 0) {
    bookmarks.splice(index, 1);
    localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(bookmarks));
    return false; // Removed
  } else {
    bookmarks.push(post);
    localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(bookmarks));
    return true; // Added
  }
};

export const isPostBookmarked = (postId) => {
  const bookmarks = getBookmarks();
  return bookmarks.some(b => b.id === postId);
};