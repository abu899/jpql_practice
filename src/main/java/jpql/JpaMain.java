package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            member.setMemberType(MemberType.ADMIN);
            em.persist(member);


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }

    public static void typedQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m ", Member.class);
        Query query1 = em.createQuery("select m.age, m.username from Member m ");

        List<Member> resultList = query.getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1.getUsername() = " + member1.getUsername());
        }
    }

    public static void parameterQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m where m.username = :username", Member.class);
        query.setParameter("username", "member1");
        List<Member> resultList = query.getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1.getUsername() = " + member1.getUsername());
        }
    }

    public static void dtoQuery(EntityManager em) {
        TypedQuery<MemberDTO> querydto = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m ", MemberDTO.class);
        List<MemberDTO> resultList1 = querydto.getResultList();

        for (MemberDTO memberDTO : resultList1) {
            System.out.println("memberDTO.getUsername() = " + memberDTO.getUsername());
            System.out.println("memberDTO.getAge() = " + memberDTO.getAge());
        }
    }

    public static void pagingQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m ", Member.class)
                .setFirstResult(1)
                .setMaxResults(10);
        List<Member> resultList = query.getResultList();
        for (Member member : resultList) {
            System.out.println("member.getUsername() = " + member.getUsername());
        }
    }

    public static void joinQuery(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member2");
        member.setAge(20);
        member.setTeam(team);
        em.persist(member);

        em.flush();
        em.clear();

        String query = "select m from Member m inner join m.team t";
        List<Member> resultList = em.createQuery(query, Member.class).getResultList();

    }

    public static void enumWithQuery(EntityManager em) {
        String query = "select m from Member m where m.memberType = jpql.MemberType.ADMIN";
        List<Member> resultList = em.createQuery(query, Member.class).getResultList();

        String queryWithParameter = "select m from Member m where m.memberType = :userType";
        List<Member> resultList2 = em.createQuery(queryWithParameter, Member.class)
                .setParameter("userType", MemberType.ADMIN)
                .getResultList();
    }

    public static void caseQuery(EntityManager em) {
        String query =
                "select " +
                "case when m.age <= 10 then '학생요금' " +
                "   when m.age >= 60 then '경로요금' " +
                "   else '일반요금' end " +
                "from Member m";
        List<String> resultList = em.createQuery(query, String.class).getResultList();
    }

    public static void basicFunctionQuery(EntityManager em) {
        String query = "select concat('a', 'b') From Member m";
        String locateQuery = "select locate('de', 'abcdefgf') from Member m";

        List<String> resultList = em.createQuery(query, String.class).getResultList();
    }

    public static void userDefinedFunctionQuery(EntityManager em) {

        String query = "select function('group_concat', m.username) from Member m";

        List<String> resultList = em.createQuery(query, String.class).getResultList();
        for (String s : resultList) {
            System.out.println("s = " + s);
        }
    }
}
